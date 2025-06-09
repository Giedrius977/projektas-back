package lt.ca.javau12.furnibay;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;

@Entity
public class ContactRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private String email;
    private String message;
    
    @Column(name = "converted_to_project")
    private boolean convertedToProject;
    
    private String status;
    
    @Column(name = "delivery_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deliveryDate;
    
    private String orderPrice;
    private String notes;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String file;

    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "contactRequest", cascade = CascadeType.ALL)
    @JsonBackReference
    private Project project;
    


    // Getteriai ir setteriai

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isConvertedToProject() { return convertedToProject; }
    public void setConvertedToProject(boolean convertedToProject) { this.convertedToProject = convertedToProject; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getOrderPrice() { return orderPrice; }
    public void setOrderPrice(String orderPrice) { this.orderPrice = orderPrice; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getFile() { return file; }
    public void setFile(String file) { this.file = file; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
}
