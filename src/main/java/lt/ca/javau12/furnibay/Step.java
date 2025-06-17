package lt.ca.javau12.furnibay;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "steps")
public class Step {
	
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Step title cannot be blank")
    @Size(max = 100, message = "Step title cannot exceed 100 characters")
    private String title;

    @Column(nullable = false)
    private boolean done = false;

    @Column(length = 50)
    private String department;
    
    @Column(name = "due_date")
    private LocalDate dueDate;
    
    @Column(name = "step_order")
    private Integer order;
    
    @Enumerated(EnumType.STRING)
    private Priority priority;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonBackReference
    private Project project;
    
    @Version
    private Long version;
    
    // Konstruktoriai
    public Step() {}
    
    public Step(String title, Project project, String department, Priority priority, Integer order) {
        this.title = title;
        this.project = project;
        this.department = department;
        this.priority = priority;
        this.order = order;
        this.done = false;
    }


    // Getteriai ir setteriai
  
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    
    
	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}
	
	 @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (!(o instanceof Step)) return false;
	        Step step = (Step) o;
	        return id != null && id.equals(step.id);
	    }

	    @Override
	    public int hashCode() {
	        return Objects.hash(getClass());
	    }

	    @Override
	    public String toString() {
	        return "Step{" +
	                "id=" + id +
	                ", title='" + title + '\'' +
	                ", done=" + done +
	                ", order=" + order +
	                '}';
	    }
	}
