package org.mybatis.spring.aot;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.dialect.helper.MySqlDialect;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.builder.CacheRefResolver;
import org.apache.ibatis.builder.ResultMapResolver;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.builder.annotation.MethodResolver;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.NullCacheKey;
import org.apache.ibatis.cache.decorators.*;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.javassist.util.proxy.ProxyFactory;
import org.apache.ibatis.javassist.util.proxy.RuntimeSupport;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.commons.JakartaCommonsLoggingImpl;
import org.apache.ibatis.logging.jdbc.*;
import org.apache.ibatis.logging.jdk14.Jdk14LoggingImpl;
import org.apache.ibatis.logging.log4j.Log4jImpl;
import org.apache.ibatis.logging.log4j2.Log4j2AbstractLoggerImpl;
import org.apache.ibatis.logging.log4j2.Log4j2Impl;
import org.apache.ibatis.logging.log4j2.Log4j2LoggerImpl;
import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.defaults.RawLanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.slf4j.SLF4JLogger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Stream;

/**
 * Register invariant hints for Mybatis that are presumed the same for all applications
 *
 * @author Josh Long
 */
class GlobalBeanFactoryInitializationAotProcessor implements BeanFactoryInitializationAotProcessor {

	private final PathMatchingResourcePatternResolver resourcePatternResolver;

	private final Logger log = LoggerFactory.getLogger(getClass());

	GlobalBeanFactoryInitializationAotProcessor(PathMatchingResourcePatternResolver patternResolver) {
		this.resourcePatternResolver = patternResolver;
	}

	private void registerProxies(RuntimeHints hints) {
		var proxies = Set.of(Set.of(Connection.class.getName()), Set.of(SqlSession.class.getName()),
				Set.of(PreparedStatement.class.getName(), CallableStatement.class.getName()),
				Set.of(ParameterizedType.class.getName(),
						"org.springframework.core.SerializableTypeWrapper$SerializableTypeProxy",
						java.io.Serializable.class.getName()),
				Set.of(TypeVariable.class.getName(),
						"org.springframework.core.SerializableTypeWrapper$SerializableTypeProxy",
						java.io.Serializable.class.getName()),
				Set.of(WildcardType.class.getName(),
						"org.springframework.core.SerializableTypeWrapper$SerializableTypeProxy",
						java.io.Serializable.class.getName()));
		AotUtils.debug("global proxies", proxies);
		for (var p : proxies) {
			var parts = p.stream().map(TypeReference::of).toArray(TypeReference[]::new);
			hints.proxies().registerJdkProxy(parts);
		}
	}

	private static Resource newResourceFor(Resource in) {
		try {
			var marker = "jar!";
			var p = in.getURL().toExternalForm();
			var rest = p.substring(p.lastIndexOf(marker) + marker.length());
			return new ClassPathResource(rest);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void registerResources(RuntimeHints hints) throws IOException {

		var resources = new HashSet<Resource>();
		var config = Stream
			.of("org/apache/ibatis/builder/xml/*.dtd", "org/apache/ibatis/builder/xml/*.xsd",
					"org/mybatis/spring/config/*.xsd")
			.map(p -> ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + p)
			.flatMap(p -> {
				try {
					return Stream.of(this.resourcePatternResolver.getResources(p));
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
			.map(GlobalBeanFactoryInitializationAotProcessor::newResourceFor)
			.filter(Resource::exists)
			.toList();

		resources.addAll(config);

		AotUtils.debug("resources", resources);
		for (var r : resources) {
			hints.resources().registerResource(r);
		}
//		hints.resources().registerResource(new ClassPathResource("nacos-version.txt"));
	}

	private void registerGlobalTypeHints(RuntimeHints hints) {

		var caches = Set.of(Cache.class, LruCache.class, BlockingCache.class, SerializedCache.class, FifoCache.class,
				NullCacheKey.class, PerpetualCache.class, CacheKey.class, WeakCache.class, TransactionalCache.class,
				SynchronizedCache.class, LoggingCache.class);

		var collections = Set.of(AbstractList.class, List.class, RandomAccess.class, Cloneable.class, Collection.class,
				TreeSet.class, SortedSet.class, Iterator.class, ArrayList.class, HashSet.class, Set.class, Map.class);

		var loggers = Set.of(Log4jImpl.class, Log4j2Impl.class, Log4j2LoggerImpl.class, Log4j2AbstractLoggerImpl.class,
				NoLoggingImpl.class, SLF4JLogger.class, StdOutImpl.class, BaseJdbcLogger.class, ConnectionLogger.class,
				PreparedStatementLogger.class, ResultSetLogger.class, StatementLogger.class, Jdk14LoggingImpl.class,
				JakartaCommonsLoggingImpl.class, Slf4jImpl.class);

		var annotations = Set.of(Select.class, Update.class, Insert.class, Delete.class, SelectProvider.class,
				UpdateProvider.class, InsertProvider.class, CacheNamespace.class, Flush.class, DeleteProvider.class,
				Options.class, Options.FlushCachePolicy.class, Many.class, Mapper.class, One.class, Property.class,
				Result.class, Results.class);

		var pageHelpers = Set.of(PageHelper.class, PageInfo.class, RowBounds.class, Page.class,
				org.apache.ibatis.executor.Executor.class, org.apache.ibatis.executor.BaseExecutor.class,
				org.apache.ibatis.executor.BatchExecutor.class, org.apache.ibatis.executor.ReuseExecutor.class,
				org.apache.ibatis.session.Configuration.class, org.apache.ibatis.type.TypeHandlerRegistry.class,
				org.apache.ibatis.mapping.BoundSql.class, org.apache.ibatis.mapping.Environment.class,
				org.apache.ibatis.transaction.Transaction.class, org.apache.ibatis.transaction.TransactionFactory.class,
				org.apache.ibatis.transaction.managed.ManagedTransaction.class, org.apache.ibatis.transaction.managed.ManagedTransactionFactory.class, MySqlDialect.class);
		var memberCategories = MemberCategory.values();

		var classesForReflection = new HashSet<Class<?>>();

		classesForReflection.addAll(caches);
		classesForReflection.addAll(annotations);
		classesForReflection.addAll(loggers);
		classesForReflection.addAll(collections);
		classesForReflection.addAll(pageHelpers);
//		classesForReflection.add(RpcClientTlsConfig.class);
//		classesForReflection.add(TlsConfig.class);

		classesForReflection.addAll(Set.of(Serializable.class, SpringBootVFS.class, PerpetualCache.class, Cursor.class,
				Optional.class, LruCache.class, MethodHandles.class, Date.class, HashMap.class, CacheRefResolver.class,
				XNode.class, ResultFlag.class, ResultMapResolver.class, MapperScannerConfigurer.class,
				MethodResolver.class, ProviderMethodResolver.class, ProviderContext.class,
				MapperAnnotationBuilder.class, Logger.class, LogFactory.class, RuntimeSupport.class, Log.class,
				SqlSessionTemplate.class, SqlSessionFactory.class, SqlSessionFactoryBean.class, ProxyFactory.class,
				XMLLanguageDriver.class, RawLanguageDriver.class, Configuration.class, String.class, int.class,
				Number.class, Integer.class, long.class, Long.class, short.class, Short.class, byte.class, Byte.class,
				float.class, Float.class, boolean.class, Boolean.class, double.class, Double.class));

		AotUtils.debug("global types for reflection", classesForReflection);

		for (var c : classesForReflection) {
			hints.reflection().registerType(c, memberCategories);
			if (AotUtils.isSerializable(c)) {
				hints.serialization().registerType(TypeReference.of(c.getName()));
				if (log.isDebugEnabled())
					log.debug("the type " + c.getName() + " is serializable");
			}
		}
		//pageHelper
		hints.proxies().registerJdkProxy(org.apache.ibatis.executor.Executor.class);
//		hints.serialization().registerType(TypeReference.of(com.alibaba.nacos.common.remote.client.RpcClientTlsConfig.class));

	}

	@Override
	public BeanFactoryInitializationAotContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory) {
		return (generationContext, beanFactoryInitializationCode) -> {
			try {
				var hints = generationContext.getRuntimeHints();
				registerResources(hints);
				registerGlobalTypeHints(hints);
				registerProxies(hints);
			} //
			catch (Throwable throwable) {
				throw new RuntimeException(throwable);
			}
		};
	}

}
