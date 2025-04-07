package dto;


import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Project {
    private User leader;
    private String shortName;
    private String ringId;
    private String name;
    private String id;
}
