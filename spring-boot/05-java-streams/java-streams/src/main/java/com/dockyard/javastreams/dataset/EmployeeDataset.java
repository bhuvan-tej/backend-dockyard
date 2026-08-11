package com.dockyard.javastreams.dataset;

import com.dockyard.javastreams.domain.Employee;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * EmployeeDataset — one fixed, realistic list of 20 employees, shared by
 * every demo endpoint in this app. Fixed data (not random) means every
 * response is reproducible and easy to reason about by eye: group by
 * department, and you can count the departments yourself; filter by salary,
 * and you can check the result against the table below.
 */
@Component
public class EmployeeDataset {

    private final List<Employee> employees = List.of(
            new Employee(1, "Alice Chen", "Engineering", "Bengaluru", 29, "F", 95000, 2019),
            new Employee(2, "Brian Kumar", "Engineering", "Bengaluru", 34, "M", 118000, 2015),
            new Employee(3, "Carla Mendes", "Engineering", "Pune", 26, "F", 82000, 2022),
            new Employee(4, "David O'Neil", "Engineering", "Remote", 41, "M", 142000, 2011),
            new Employee(5, "Esha Verma", "Sales", "Mumbai", 31, "F", 76000, 2018),
            new Employee(6, "Farid Khan", "Sales", "Delhi", 45, "M", 88000, 2008),
            new Employee(7, "Grace Lin", "Sales", "Mumbai", 24, "F", 61000, 2023),
            new Employee(8, "Harish Rao", "HR", "Bengaluru", 38, "M", 71000, 2013),
            new Employee(9, "Isha Kapoor", "HR", "Delhi", 29, "F", 68000, 2019),
            new Employee(10, "Jonas Meyer", "Marketing", "Remote", 33, "M", 79000, 2016),
            new Employee(11, "Kavya Iyer", "Marketing", "Pune", 27, "F", 73000, 2021),
            new Employee(12, "Liu Wei", "Marketing", "Bengaluru", 36, "M", 91000, 2014),
            new Employee(13, "Maria Silva", "Finance", "Mumbai", 42, "F", 132000, 2010),
            new Employee(14, "Nikhil Joshi", "Finance", "Delhi", 30, "M", 98000, 2017),
            new Employee(15, "Olga Petrova", "Finance", "Remote", 25, "F", 70000, 2022),
            new Employee(16, "Pranav Desai", "Engineering", "Pune", 39, "M", 128000, 2012),
            new Employee(17, "Queenie Fernandes", "Sales", "Mumbai", 28, "F", 69000, 2020),
            new Employee(18, "Rohit Bansal", "HR", "Bengaluru", 46, "M", 74000, 2007),
            new Employee(19, "Sara Ahmed", "Marketing", "Delhi", 23, "F", 58000, 2024),
            new Employee(20, "Tomás Garcia", "Finance", "Bengaluru", 35, "M", 105000, 2016)
    );

    public List<Employee> all() {
        return employees;
    }

    /** Used by the post-Java-8 lookup demos (Stream.ofNullable / Optional.stream) — deliberately absent for some ids. */
    public Optional<Employee> findById(long id) {
        return employees.stream().filter(e -> e.id() == id).findFirst();
    }
}