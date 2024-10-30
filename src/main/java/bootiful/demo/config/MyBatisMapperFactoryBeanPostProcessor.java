package bootiful.demo.config;

import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

/**
 * @author chenyaolin 2024/10/29 11:26
 **/
@Component
public class MyBatisMapperFactoryBeanPostProcessor  implements MergedBeanDefinitionPostProcessor,
        BeanFactoryAware {


    private static final String MAPPER_FACTORY_BEAN = "org.mybatis.spring.mapper.MapperFactoryBean";

    private ConfigurableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
//        System.out.println("==============================");
        this.beanFactory = (ConfigurableBeanFactory) beanFactory;
    }


    private void resolveMapperFactoryBeanTypeIfNecessary(RootBeanDefinition beanDefinition) {
        if (!beanDefinition.hasBeanClass() || !MapperFactoryBean.class.isAssignableFrom(
                 beanDefinition.getBeanClass())) {
            return;
        }
        if (beanDefinition.getResolvableType().hasUnresolvableGenerics()) {
            Class<?> mapperInterface = getMapperInterface(beanDefinition);
            if (mapperInterface != null) {
//                System.out.println("=============================="+this.beanFactory.getBeanClassLoader());

                // Exposes a generic type information to context for prevent early initializing
                beanDefinition
                        .setTargetType(
                                ResolvableType.forClassWithGenerics(beanDefinition.getBeanClass(),
                                        mapperInterface));
                //适配spring boot 3.2.x 新增
                ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
                constructorArgumentValues.addGenericArgumentValue(mapperInterface);
                beanDefinition.setConstructorArgumentValues(constructorArgumentValues);
            }
        }
    }

    private Class<?> getMapperInterface(RootBeanDefinition beanDefinition) {
        try {
            return (Class<?>) beanDefinition.getPropertyValues().get("mapperInterface");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition,
                                                Class<?> beanType, String beanName) {
        if (ClassUtils.isPresent(MAPPER_FACTORY_BEAN, this.beanFactory.getBeanClassLoader())) {

            resolveMapperFactoryBeanTypeIfNecessary(beanDefinition);
        }
    }
}
