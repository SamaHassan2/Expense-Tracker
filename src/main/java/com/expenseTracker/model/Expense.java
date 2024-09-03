package com.expenseTracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Data
@Getter
@Setter
@Table(name = "expenses")
public class Expense {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	
	@Column(name = "user_id")
	private Long userId;
	
	
	@Column(name = "category_id")
	private Long categoryId;
	
	@Column(name = "amount")
	private BigDecimal amount;
	
	@Column(name = "date")
	private LocalDate expenseDate;
	
	@Column(name = "description")
	private String description;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

}
