package com.expenseTracker.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.expenseTracker.model.ExpenseCategories;
import com.expenseTracker.service.ExpenseCategoriesService;

@RestController
@RequestMapping("/api/categories")
public class ExpenseCategoriesController {

	@Autowired
	private ExpenseCategoriesService expenseCategoriesService;

	@PostMapping("/add")
	public ExpenseCategories addCategory(@RequestParam String name) {
		ExpenseCategories expCategory = new ExpenseCategories();
		expCategory.setName(name);
		return expenseCategoriesService.addCategory(expCategory);
		 
	}

	@GetMapping("/all")
	public List<ExpenseCategories> getAllCategories() {
		return expenseCategoriesService.getAllCategories();
		 
	}

}
