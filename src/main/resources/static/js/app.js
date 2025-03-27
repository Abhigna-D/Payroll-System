// Base URL for API
const API_BASE_URL = '/api/employees';

// DOM Elements
const employeeTableBody = document.getElementById('employeeTableBody');
const employeeDetails = document.getElementById('employeeDetails');
const createEmployeeForm = document.getElementById('createEmployeeForm');
const employeeTypeSelect = document.getElementById('employeeType');
const hourlyRateField = document.getElementById('hourlyRateField');

// Event Listeners
document.addEventListener('DOMContentLoaded', function() {
    // Toggle hourly rate field based on employee type
    employeeTypeSelect.addEventListener('change', function() {
        if (this.value === 'parttime') {
            hourlyRateField.style.display = 'block';
        } else {
            hourlyRateField.style.display = 'none';
        }
    });

    // Show employee details when view button is clicked
    document.querySelectorAll('.view-employee').forEach(button => {
        button.addEventListener('click', function() {
            // In a real application, you would fetch employee details from the server
            // For now, we'll just show the details div with sample data
            const row = this.closest('tr');
            const employeeId = row.cells[0].textContent;
            const name = row.cells[1].textContent;
            const position = row.cells[2].textContent;
            const department = row.cells[3].textContent;

            document.getElementById('detailsEmployeeName').textContent = name;
            document.getElementById('detailsEmployeeID').textContent = employeeId;
            document.getElementById('detailsName').textContent = name;
            document.getElementById('detailsPosition').textContent = position;
            document.getElementById('detailsDepartment').textContent = department;
            document.getElementById('detailsBasePay').textContent = '4500.00';  // Sample data
            document.getElementById('detailsNetPay').textContent = '3800.00';  // Sample data

            employeeDetails.style.display = 'block';
        });
    });

    // Handle create employee form submission
    createEmployeeForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        // In a real application, you would collect form data and send it to the server
        alert('Employee created successfully!');
        
        // Reset the form
        this.reset();
    });

    // Sample code to fetch employees from the server (uncomment in real application)
    /*
    fetchEmployees();

    function fetchEmployees() {
        fetch(API_BASE_URL)
            .then(response => response.json())
            .then(data => {
                populateEmployeeTable(data);
            })
            .catch(error => {
                console.error('Error fetching employees:', error);
            });
    }

    function populateEmployeeTable(employees) {
        employeeTableBody.innerHTML = '';
        
        employees.forEach(employee => {
            const row = document.createElement('tr');
            row.className = 'employee-row';
            row.innerHTML = `
                <td>${employee.employeeID}</td>
                <td>${employee.name}</td>
                <td>${employee.position}</td>
                <td>${employee.department.name}</td>
                <td>
                    <button class="btn btn-sm btn-info view-employee" data-id="${employee.id}">View</button>
                    <button class="btn btn-sm btn-warning edit-employee" data-id="${employee.id}">Edit</button>
                    <button class="btn btn-sm btn-danger delete-employee" data-id="${employee.id}">Delete</button>
                </td>
            `;
            employeeTableBody.appendChild(row);
        });

        // Add event listeners to buttons
        document.querySelectorAll('.view-employee').forEach(button => {
            button.addEventListener('click', function() {
                const employeeId = this.getAttribute('data-id');
                fetchEmployeeDetails(employeeId);
            });
        });
    }

    function fetchEmployeeDetails(id) {
        fetch(`${API_BASE_URL}/${id}`)
            .then(response => response.json())
            .then(employee => {
                // Populate employee details
                document.getElementById('detailsEmployeeName').textContent = employee.name;
                document.getElementById('detailsEmployeeID').textContent = employee.employeeID;
                document.getElementById('detailsName').textContent = employee.name;
                document.getElementById('detailsPosition').textContent = employee.position;
                document.getElementById('detailsDepartment').textContent = employee.department.name;
                document.getElementById('detailsBasePay').textContent = employee.salaryDetails.basePay;
                document.getElementById('detailsNetPay').textContent = employee.salaryDetails.netPay;
                
                employeeDetails.style.display = 'block';
            });
    }
    */

    // Initialize Bootstrap tabs
    var tabElements = document.querySelectorAll('#mainTabs button');
    tabElements.forEach(function(tabElement) {
        tabElement.addEventListener('click', function(event) {
            event.preventDefault();
            var tabTarget = document.querySelector(this.getAttribute('data-bs-target'));
            
            // Hide all tab panes
            document.querySelectorAll('.tab-pane').forEach(function(pane) {
                pane.classList.remove('show', 'active');
            });
            
            // Remove active class from all tabs
            tabElements.forEach(function(tab) {
                tab.classList.remove('active');
            });
            
            // Show the selected tab pane
            tabTarget.classList.add('show', 'active');
            
            // Set the clicked tab as active
            this.classList.add('active');
        });
    });

    // Handle tax calculation
    document.getElementById('calculateTax').addEventListener('click', function() {
        // Get tax input values
        const tds = parseFloat(document.getElementById('tds').value) || 0;
        const professionalTax = parseFloat(document.getElementById('professionalTax').value) || 0;
        const pf = parseFloat(document.getElementById('pf').value) || 0;
        const insurance = parseFloat(document.getElementById('insurance').value) || 0;
        const esi = parseFloat(document.getElementById('esi').value) || 0;
        const loanRepayment = parseFloat(document.getElementById('loanRepayment').value) || 0;
        
        // Calculate total tax and deductions
        const totalTax = tds + professionalTax;
        const totalDeductions = pf + insurance + esi + loanRepayment;
        
        alert(`Total Tax: ${totalTax.toFixed(2)}\nTotal Deductions: ${totalDeductions.toFixed(2)}`);
    });

    // Handle salary calculation
    document.getElementById('calculateNetPay').addEventListener('click', function() {
        // Get salary input values
        const basePay = parseFloat(document.getElementById('salaryBasePay').value) || 0;
        const overTime = parseFloat(document.getElementById('overTime').value) || 0;
        const bonuses = parseFloat(document.getElementById('bonuses').value) || 0;
        
        // Assume tax rate for this example (in a real app, you would get this from the server)
        const taxRate = 0.2; // 20%
        
        // Calculate net pay
        const grossPay = basePay + overTime + bonuses;
        const deductions = grossPay * taxRate;
        const netPay = grossPay - deductions;
        
        // Update the net pay field
        document.getElementById('netPay').value = netPay.toFixed(2);
    });

    // Handle timesheet submission button
    document.getElementById('submitTimesheetBtn').addEventListener('click', function() {
        const employeeId = document.getElementById('timesheetEmployee').value;
        const timesheetDetails = document.getElementById('timesheetDetails').value;
        
        if (!employeeId || !timesheetDetails) {
            alert('Please fill in all required fields.');
            return;
        }
        
        // In a real application, you would send this data to the server
        alert('Timesheet submitted successfully!');
        
        // Close the modal
        var modal = bootstrap.Modal.getInstance(document.getElementById('timesheetModal'));
        modal.hide();
    });
});

/* 
 * API Client Functions 
 * Uncomment and use these in a real application to connect to your Spring Boot backend
 */

/*
// Fetch all employees
function getAllEmployees() {
    return fetch(`${API_BASE_URL}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        });
}

// Get employee by ID
function getEmployeeById(id) {
    return fetch(`${API_BASE_URL}/${id}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        });
}

// Create a new employee
function createEmployee(employeeData) {
    return fetch(`${API_BASE_URL}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(employeeData),
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    });
}

// Update an employee
function updateEmployee(id, employeeData) {
    return fetch(`${API_BASE_URL}/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(employeeData),
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    });
}

// Delete an employee
function deleteEmployee(id) {
    return fetch(`${API_BASE_URL}/${id}`, {
        method: 'DELETE',
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.status === 204 ? null : response.json();
    });
}

// Get employees by department
function getEmployeesByDepartment(deptId) {
    return fetch(`${API_BASE_URL}/department/${deptId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        });
}

// View salary details
function viewSalaryDetails(id) {
    return fetch(`${API_BASE_URL}/${id}/salary`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        });
}

// Submit tax details
function submitTaxDetails(id, taxData) {
    return fetch(`${API_BASE_URL}/${id}/tax`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(taxData),
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    });
}

// Request salary correction
function requestSalaryCorrection(id, salaryData) {
    return fetch(`${API_BASE_URL}/${id}/salary-correction`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(salaryData),
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.text();
    });
}

// Submit timesheet
function submitTimesheet(id, timesheetData) {
    return fetch(`${API_BASE_URL}/${id}/timesheet`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(timesheetData),
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.text();
    });
}
*/