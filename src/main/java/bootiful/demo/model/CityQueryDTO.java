package bootiful.demo.model;

import lombok.*;

import java.io.Serializable;

/**
 * @author chenyaolin 2024/10/23 14:29
 **/
@Getter
@Setter
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class CityQueryDTO extends PageDTO implements Serializable {


    String name;

    String state;

    String country;

}
