package lt.ca.javau12.furnibay;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;


@Entity
@Table(name = "projects")
@JsonInclude(JsonInclude.Include.NON_NULL)  // Nerodyti null reikšmių
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference("user-projects")
    private User user;
  
    @NotBlank
	private String description;

    private String materials;
    
    private String status;
    
    //@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Step> steps = new ArrayList<>();

    @OneToOne
    @JsonManagedReference
    private ContactRequest contactRequest;
    
    
    private LocalDate deliveryDate; // Sutampa su ContactRequest
    
    private String orderPrice;      // Sutampa su ContactRequest
	
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
        return contactRequest != null ? contactRequest.getStatus() : this.status;
    }

    public void setContactRequest(ContactRequest contactRequest) {
        this.contactRequest = contactRequest;
        if (contactRequest != null) {
            this.status = contactRequest.getStatus();  // pradinis statuso sinchronizavimas
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

	public String getMaterials() {
		return materials;
	}

	public void setMaterials(String materials) {
		this.materials = materials;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public List<Step> getSteps() {
	    return steps;
	}

	public void setSteps(List<Step> steps) {
	    this.steps = steps;
	}

}