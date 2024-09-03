package com.expenseTracker.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expenseTracker.model.ExpenseCategories;
import com.expenseTracker.repository.ExpenseCategoriesRepository;

@Service
public class ExpenseCategoriesService {

	@Autowired
	private ExpenseCategoriesRepository expenseCategoriesRepository;

	public ExpenseCategories addCategory(ExpenseCategories name) {
		return expenseCategoriesRepository.save(name);
	}

	public List<ExpenseCategories> getAllCategories() {
		return expenseCategoriesRepository.findAll();
	}

}
