package bootiful.demo.model;

import lombok.*;

/**
 * @author chenyaolin 2024/10/23 14:30
 **/
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PageDTO {
    private Integer pageNum;
    private Integer pageSize;
}
