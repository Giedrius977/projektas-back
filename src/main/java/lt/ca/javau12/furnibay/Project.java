package lt.ca.javau12.furnibay;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @ManyToOne
    private User user;
    
    @NotBlank
	private String description;
    
    private String status;

    private Double estimatedPrice;

    private LocalDate estimatedDeadline;

    private String materials;

    private String notes;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Step> steps;
    
    
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Double getEstimatedPrice() {
		return estimatedPrice;
	}

	public void setEstimatedPrice(Double estimatedPrice) {
		this.estimatedPrice = estimatedPrice;
	}

	public LocalDate getEstimatedDeadline() {
		return estimatedDeadline;
	}

	public void setEstimatedDeadline(LocalDate estimatedDeadline) {
		this.estimatedDeadline = estimatedDeadline;
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
