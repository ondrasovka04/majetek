package cz.majetek.attachment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentDto {
    private Integer id;
    private Integer cardId;
    private String name;
    private String filePath;
}
