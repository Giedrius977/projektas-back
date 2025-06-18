package lt.ca.javau12.furnibay;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Maps to auto-increment primary key
    
    //@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    //@JsonManagedReference
    @OneToOne
    @JoinColumn(name = "contact_request_id")
    private ContactRequest contactRequest;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    // Other fields matching your table columns
    private String name;
    private String description;
    private String status;
    private LocalDate createdAt;
    private LocalDate deliveryDate;
    private String orderPrice;
    private String notes;
    
    
    

    
    
   

	public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(String orderPrice) {
        this.orderPrice = orderPrice;
    }

    public ContactRequest getContactRequest() {
        return contactRequest;
    }

    public void setStatus(String status) {
        this.status = status;
        if (this.contactRequest != null) {
            this.contactRequest.setStatus(status); // Sinchronizuojame abi puses
        }
    }

    public String getStatus() {
        return this.status != null ? this.status : 
               (contactRequest != null ? contactRequest.getStatus() : null);
    }

    public void setContactRequest(ContactRequest contactRequest) {
        this.contactRequest = contactRequest;
        if (contactRequest != null && contactRequest.getProject() != this) {
            contactRequest.setProject(this);
        }
    }

    public String getClientName() {
        return user != null ? user.getName() : null;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}
    
}