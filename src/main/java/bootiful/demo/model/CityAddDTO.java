package bootiful.demo.model;

import lombok.*;

import java.io.Serializable;

/**
 * @author chenyaolin 2024/10/22 16:55
 **/

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CityAddDTO implements Serializable {

    Integer id;

    String name;

    String state;

    String country;

}