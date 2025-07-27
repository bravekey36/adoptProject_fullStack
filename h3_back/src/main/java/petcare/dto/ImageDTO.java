package petcare.dto;

import java.time.LocalDateTime;

public class ImageDTO {
    // chatbot_aivet_image 테이블용
    private int imageId;           // image_id (INT AUTO_INCREMENT)
    private int messageId;         // message_id (INT)
    private String imageFilename;  // image_filename (VARCHAR 255)
    private String imagePath;      // image_path (VARCHAR 500)
    private String imageComment;   // image_comment (VARCHAR 100)
    private LocalDateTime createdAt;   // created_at
    private LocalDateTime updatedAt;   // updated_at

    public ImageDTO() {}

    // Getter/Setter
    public int getImageId() { return imageId; }
    public void setImageId(int imageId) { this.imageId = imageId; }

    public int getMessageId() { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }

    public String getImageFilename() { return imageFilename; }
    public void setImageFilename(String imageFilename) { this.imageFilename = imageFilename; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getImageComment() { return imageComment; }
    public void setImageComment(String imageComment) { this.imageComment = imageComment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}