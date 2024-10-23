package bootiful.demo.entity;

import lombok.*;

/**
 * @author chenyaolin 2024/10/22 16:55
 **/
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class City implements java.io.Serializable {

    Integer id;

    String name;

    String state;

    String country;

}