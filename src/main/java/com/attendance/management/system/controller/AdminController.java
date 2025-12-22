package com.attendance.management.system.controller;

import com.attendance.management.system.entity.Application;
import com.attendance.management.system.entity.Department;
import com.attendance.management.system.entity.Employee;
import com.attendance.management.system.entity.PositionConfig;
import com.attendance.management.system.service.DepartmentService;
import com.attendance.management.system.service.EmployeeService;
import com.attendance.management.system.service.PositionConfigService;
import com.attendance.management.system.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private PositionConfigService positionConfigService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ApplicationService applicationService;

    // ========== 员工管理 ==========
    @GetMapping("/employees")
    public ResponseEntity<List<Map<String, Object>>> getEmployees() {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            List<Map<String, Object>> result = new ArrayList<>();
            for (Employee emp : employees) {
                Map<String, Object> empMap = new HashMap<>();
                empMap.put("employeeId", emp.getEid());
                empMap.put("name", emp.getName());
                empMap.put("phone", emp.getPhone());
                // 将字符串ID转换为数字（用于前端显示，前端可能仍使用数字）
                try {
                    empMap.put("departmentId", Integer.parseInt(emp.getDid().replaceAll("[^0-9]", "")));
                } catch (Exception e) {
                    empMap.put("departmentId", 0);
                }
                try {
                    empMap.put("positionId", Integer.parseInt(emp.getPid().replaceAll("[^0-9]", "")));
                } catch (Exception e) {
                    empMap.put("positionId", 0);
                }
                empMap.put("role", emp.getRole());
                empMap.put("status", "ACTIVE"); // 默认状态
                
                // 查询部门名称
                Department dept = departmentService.getDepartmentById(emp.getDid());
                if (dept != null) {
                    empMap.put("departmentName", dept.getDName());
                }
                
                // 查询职务名称
                PositionConfig pos = positionConfigService.getPositionConfigById(emp.getPid());
                if (pos != null) {
                    empMap.put("positionName", pos.getPName());
                }
                
                result.add(empMap);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "获取员工列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    @PostMapping("/employees")
    public ResponseEntity<Map<String, Object>> createEmployee(@RequestBody Map<String, Object> request) {
        try {
            // 验证必填字段
            String name = (String) request.get("name");
            if (name == null || name.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "员工姓名不能为空");
                error.put("success", false);
                return ResponseEntity.status(400).body(error);
            }
            
            // 验证部门ID和职务ID
            Object deptIdObj = request.get("departmentId");
            Object posIdObj = request.get("positionId");
            
            if (deptIdObj == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "部门ID不能为空");
                error.put("success", false);
                return ResponseEntity.status(400).body(error);
            }
            
            if (posIdObj == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "职务ID不能为空");
                error.put("success", false);
                return ResponseEntity.status(400).body(error);
            }
            
            int deptIdNum = ((Number) deptIdObj).intValue();
            int posIdNum = ((Number) posIdObj).intValue();
            
            if (deptIdNum <= 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "部门ID无效");
                error.put("success", false);
                return ResponseEntity.status(400).body(error);
            }
            
            if (posIdNum <= 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "职务ID无效");
                error.put("success", false);
                return ResponseEntity.status(400).body(error);
            }
            
            // 验证部门和职务是否存在
            String deptIdStr = String.format("D%09d", deptIdNum);
            String posIdStr = String.format("P%09d", posIdNum);
            
            Department dept = departmentService.getDepartmentById(deptIdStr);
            if (dept == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "部门不存在");
                error.put("success", false);
                return ResponseEntity.status(400).body(error);
            }
            
            PositionConfig pos = positionConfigService.getPositionConfigById(posIdStr);
            if (pos == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "职务不存在");
                error.put("success", false);
                return ResponseEntity.status(400).body(error);
            }
            
            Employee employee = new Employee();
            employee.setName(name.trim());
            
            // 处理Phone字段：如果为空字符串，设置为null（避免违反UNIQUE约束）
            String phone = (String) request.get("phone");
            if (phone != null && phone.trim().isEmpty()) {
                phone = null;
            }
            employee.setPhone(phone);
            
            employee.setDid(deptIdStr);
            employee.setPid(posIdStr);
            employee.setRole((String) request.getOrDefault("role", "EMPLOYEE"));
            employee.setSex("男"); // 默认值
            employee.setPassword("123456"); // 默认密码
            
            employeeService.createEmployee(employee);
            
            Map<String, Object> result = new HashMap<>();
            result.put("employeeId", employee.getEid());
            result.put("name", employee.getName());
            result.put("phone", employee.getPhone());
            result.put("departmentId", deptIdNum); // 返回数字ID给前端
            result.put("positionId", posIdNum); // 返回数字ID给前端
            result.put("role", employee.getRole());
            result.put("status", "ACTIVE");
            
            return ResponseEntity.ok(result);
        } catch (NumberFormatException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "部门ID或职务ID格式错误");
            error.put("success", false);
            return ResponseEntity.status(400).body(error);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 处理外键约束或唯一约束违反
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("Phone")) {
                errorMsg = "该手机号已被使用";
            } else if (errorMsg != null && errorMsg.contains("foreign key")) {
                errorMsg = "部门或职务不存在";
            } else {
                errorMsg = "数据完整性约束违反: " + e.getMessage();
            }
            Map<String, Object> error = new HashMap<>();
            error.put("message", errorMsg);
            error.put("success", false);
            return ResponseEntity.status(400).body(error);
        } catch (Exception e) {
            e.printStackTrace(); // 打印完整堆栈信息用于调试
            Map<String, Object> error = new HashMap<>();
            error.put("message", "创建员工失败: " + e.getMessage());
            error.put("success", false);
            return ResponseEntity.status(500).body(error);
        }
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<Map<String, Object>> updateEmployee(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            Employee employee = employeeService.getEmployeeDetails(id.intValue());
            if (employee == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "员工不存在");
                return ResponseEntity.status(404).body(error);
            }
            
            if (request.containsKey("name")) employee.setName((String) request.get("name"));
            if (request.containsKey("phone")) employee.setPhone((String) request.get("phone"));
            if (request.containsKey("departmentId")) {
                int deptIdNum = ((Number) request.get("departmentId")).intValue();
                employee.setDid(String.format("D%09d", deptIdNum));
            }
            if (request.containsKey("positionId")) {
                int posIdNum = ((Number) request.get("positionId")).intValue();
                employee.setPid(String.format("P%09d", posIdNum));
            }
            if (request.containsKey("role")) employee.setRole((String) request.get("role"));
            
            employeeService.updateEmployee(employee);
            
            Map<String, Object> result = new HashMap<>();
            result.put("employeeId", employee.getEid());
            result.put("name", employee.getName());
            result.put("phone", employee.getPhone());
            result.put("departmentId", employee.getDid());
            result.put("positionId", employee.getPid());
            result.put("role", employee.getRole());
            result.put("status", "ACTIVE");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "更新员工失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/employees/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Long id) {
        try {
            employeeService.resetPassword(id.intValue());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "密码重置成功");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "重置密码失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<Map<String, Object>> deleteEmployee(@PathVariable Long id) {
        try {
            employeeService.deleteEmployee(id.intValue());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "员工删除成功");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "删除员工失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ========== 部门管理 ==========
    @GetMapping("/departments")
    public ResponseEntity<List<Map<String, Object>>> getDepartments() {
        try {
            List<Department> departments = departmentService.getAllDepartments();
            List<Map<String, Object>> result = new ArrayList<>();
            for (Department dept : departments) {
                Map<String, Object> deptMap = new HashMap<>();
                // 将字符串ID转换为数字（用于前端显示，与getEmployees保持一致）
                try {
                    deptMap.put("id", Integer.parseInt(dept.getDid().replaceAll("[^0-9]", "")));
                } catch (Exception e) {
                    deptMap.put("id", 0);
                }
                deptMap.put("name", dept.getDName());
                deptMap.put("description", dept.getDescription());
                deptMap.put("managerId", dept.getManagerId());
                result.add(deptMap);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "获取部门列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    @PostMapping("/departments")
    public ResponseEntity<Map<String, Object>> createDepartment(@RequestBody Map<String, Object> request) {
        try {
            Department department = new Department();
            
            // 生成部门ID（10位字符串，格式：D000000001）
            String did = generateDepartmentId();
            department.setDid(did);
            department.setDName((String) request.get("name"));
            department.setDescription((String) request.get("description"));
            
            Object managerId = request.get("managerId");
            if (managerId != null) {
                if (managerId instanceof String && !((String) managerId).isEmpty()) {
                    try {
                        department.setManagerId(Integer.parseInt((String) managerId));
                    } catch (NumberFormatException e) {
                        // 忽略无效的数字
                    }
                } else if (managerId instanceof Number) {
                    department.setManagerId(((Number) managerId).intValue());
                }
            }
            
            departmentService.createDepartment(department);
            
            Map<String, Object> result = new HashMap<>();
            // 将字符串ID转换为数字（与getDepartments保持一致）
            try {
                result.put("id", Integer.parseInt(department.getDid().replaceAll("[^0-9]", "")));
            } catch (Exception e) {
                result.put("id", 0);
            }
            result.put("name", department.getDName());
            result.put("description", department.getDescription());
            result.put("managerId", department.getManagerId());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "创建部门失败: " + e.getMessage());
            error.put("success", false);
            return ResponseEntity.status(500).body(error);
        }
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<Map<String, Object>> updateDepartment(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {
        try {
            Department department = departmentService.getDepartmentById(id);
            if (department == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "部门不存在");
                return ResponseEntity.status(404).body(error);
            }
            
            if (request.containsKey("name")) department.setDName((String) request.get("name"));
            if (request.containsKey("description")) department.setDescription((String) request.get("description"));
            if (request.containsKey("managerId")) {
                Object managerId = request.get("managerId");
                if (managerId instanceof Number) {
                    department.setManagerId(((Number) managerId).intValue());
                } else if (managerId instanceof String && !((String) managerId).isEmpty()) {
                    try {
                        department.setManagerId(Integer.parseInt((String) managerId));
                    } catch (NumberFormatException e) {
                        // 忽略
                    }
                }
            }
            
            departmentService.updateDepartment(department);
            
            Map<String, Object> result = new HashMap<>();
            // 将字符串ID转换为数字（与getDepartments保持一致）
            try {
                result.put("id", Integer.parseInt(department.getDid().replaceAll("[^0-9]", "")));
            } catch (Exception e) {
                result.put("id", 0);
            }
            result.put("name", department.getDName());
            result.put("description", department.getDescription());
            result.put("managerId", department.getManagerId());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "更新部门失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Map<String, Object>> deleteDepartment(@PathVariable String id) {
        try {
            // 检查部门是否存在
            Department department = departmentService.getDepartmentById(id);
            if (department == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "部门不存在");
                error.put("success", false);
                return ResponseEntity.status(404).body(error);
            }
            
            // 检查该部门是否还有员工
            if (employeeService.hasEmployeesInDepartment(id)) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "该部门下还有员工，无法删除。请先将员工转移到其他部门或删除员工后再删除部门。");
                error.put("success", false);
                return ResponseEntity.status(400).body(error);
            }
            
            departmentService.deleteDepartment(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "部门删除成功");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 处理外键约束违反（虽然我们已经检查了，但作为双重保险）
            Map<String, Object> error = new HashMap<>();
            error.put("message", "该部门下还有员工，无法删除。请先将员工转移到其他部门或删除员工后再删除部门。");
            error.put("success", false);
            return ResponseEntity.status(400).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "删除部门失败: " + e.getMessage());
            error.put("success", false);
            return ResponseEntity.status(500).body(error);
        }
    }

    // 生成部门ID（格式：D000000001）
    private String generateDepartmentId() {
        List<Department> all = departmentService.getAllDepartments();
        int maxNum = 0;
        for (Department dept : all) {
            String did = dept.getDid();
            if (did != null && did.startsWith("D") && did.length() == 10) {
                try {
                    int num = Integer.parseInt(did.substring(1));
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException e) {
                    // 忽略
                }
            }
        }
        return String.format("D%09d", maxNum + 1);
    }

    // ========== 职务管理 ==========
    @GetMapping("/positions")
    public ResponseEntity<List<Map<String, Object>>> getPositionConfigs() {
        try {
            List<PositionConfig> positions = positionConfigService.getAllPositionConfigs();
            List<Map<String, Object>> result = new ArrayList<>();
            for (PositionConfig pos : positions) {
                Map<String, Object> posMap = new HashMap<>();
                // 将字符串ID转换为数字（用于前端显示，与getEmployees保持一致）
                try {
                    posMap.put("id", Integer.parseInt(pos.getPid().replaceAll("[^0-9]", "")));
                } catch (Exception e) {
                    posMap.put("id", 0);
                }
                posMap.put("name", pos.getPName());
                posMap.put("workStartTime", formatTime(pos.getWorkStartTime()));
                posMap.put("workEndTime", formatTime(pos.getWorkEndTime()));
                posMap.put("monthlyWorkDays", pos.getMonthlyWorkDays());
                posMap.put("dailyWorkHours", pos.getDailyWorkHours());
                posMap.put("description", pos.getDescription());
                result.add(posMap);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "获取职务配置失败: " + e.getMessage());
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    @PostMapping("/positions")
    public ResponseEntity<Map<String, Object>> createPositionConfig(@RequestBody Map<String, Object> request) {
        try {
            PositionConfig positionConfig = new PositionConfig();
            
            // 生成职务ID（10位字符串，格式：P000000001）
            String pid = generatePositionId();
            positionConfig.setPid(pid);
            positionConfig.setPName((String) request.get("name"));
            
            // 解析时间字符串为Time对象
            String startTimeStr = (String) request.get("workStartTime");
            String endTimeStr = (String) request.get("workEndTime");
            positionConfig.setWorkStartTime(Time.valueOf(startTimeStr + ":00"));
            positionConfig.setWorkEndTime(Time.valueOf(endTimeStr + ":00"));
            
            // 月工作日和每日工作时长
            Object monthlyWorkDays = request.get("monthlyWorkDays");
            if (monthlyWorkDays != null) {
                positionConfig.setMonthlyWorkDays(((Number) monthlyWorkDays).intValue());
            } else {
                positionConfig.setMonthlyWorkDays(21); // 默认值
            }

            Object dailyWorkHours = request.get("dailyWorkHours");
            if (dailyWorkHours != null) {
                positionConfig.setDailyWorkHours(BigDecimal.valueOf(((Number) dailyWorkHours).doubleValue()));
            } else {
                positionConfig.setDailyWorkHours(BigDecimal.valueOf(8.0)); // 默认值
            }

            positionConfig.setDescription((String) request.get("description"));
            
            positionConfigService.createPositionConfig(positionConfig);
            
            Map<String, Object> result = new HashMap<>();
            // 将字符串ID转换为数字（与getPositionConfigs保持一致）
            try {
                result.put("id", Integer.parseInt(positionConfig.getPid().replaceAll("[^0-9]", "")));
            } catch (Exception e) {
                result.put("id", 0);
            }
            result.put("name", positionConfig.getPName());
            result.put("workStartTime", formatTime(positionConfig.getWorkStartTime()));
            result.put("workEndTime", formatTime(positionConfig.getWorkEndTime()));
            result.put("monthlyWorkDays", positionConfig.getMonthlyWorkDays());
            result.put("dailyWorkHours", positionConfig.getDailyWorkHours());
            result.put("description", positionConfig.getDescription());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "创建职务配置失败: " + e.getMessage());
            error.put("success", false);
            return ResponseEntity.status(500).body(error);
        }
    }

    @PutMapping("/positions/{id}")
    public ResponseEntity<Map<String, Object>> updatePositionConfig(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {
        try {
            PositionConfig positionConfig = positionConfigService.getPositionConfigById(id);
            if (positionConfig == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "职务配置不存在");
                return ResponseEntity.status(404).body(error);
            }
            
            if (request.containsKey("name")) positionConfig.setPName((String) request.get("name"));
            if (request.containsKey("workStartTime")) {
                String startTimeStr = (String) request.get("workStartTime");
                positionConfig.setWorkStartTime(Time.valueOf(startTimeStr + ":00"));
            }
            if (request.containsKey("workEndTime")) {
                String endTimeStr = (String) request.get("workEndTime");
                positionConfig.setWorkEndTime(Time.valueOf(endTimeStr + ":00"));
            }
            if (request.containsKey("monthlyWorkDays")) {
                positionConfig.setMonthlyWorkDays(((Number) request.get("monthlyWorkDays")).intValue());
            }
            if (request.containsKey("dailyWorkHours")) {
                positionConfig.setDailyWorkHours(BigDecimal.valueOf(((Number) request.get("dailyWorkHours")).doubleValue()));
            }
            if (request.containsKey("description")) {
                positionConfig.setDescription((String) request.get("description"));
            }
            
            positionConfigService.updatePositionConfig(positionConfig);
            
            Map<String, Object> result = new HashMap<>();
            // 将字符串ID转换为数字（与getPositionConfigs保持一致）
            try {
                result.put("id", Integer.parseInt(positionConfig.getPid().replaceAll("[^0-9]", "")));
            } catch (Exception e) {
                result.put("id", 0);
            }
            result.put("name", positionConfig.getPName());
            result.put("workStartTime", formatTime(positionConfig.getWorkStartTime()));
            result.put("workEndTime", formatTime(positionConfig.getWorkEndTime()));
            result.put("monthlyWorkDays", positionConfig.getMonthlyWorkDays());
            result.put("dailyWorkHours", positionConfig.getDailyWorkHours());
            result.put("description", positionConfig.getDescription());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "更新职务配置失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @DeleteMapping("/positions/{id}")
    public ResponseEntity<Map<String, Object>> deletePositionConfig(@PathVariable String id) {
        try {
            // 检查职务配置是否存在
            PositionConfig positionConfig = positionConfigService.getPositionConfigById(id);
            if (positionConfig == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "职务配置不存在");
                error.put("success", false);
                return ResponseEntity.status(404).body(error);
            }
            
            // 检查该职务是否还有员工
            if (employeeService.hasEmployeesInPosition(id)) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "该职务下还有员工，无法删除。请先将员工转移到其他职务或删除员工后再删除职务。");
                error.put("success", false);
                return ResponseEntity.status(400).body(error);
            }
            
            positionConfigService.deletePositionConfig(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "职务配置删除成功");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 处理外键约束违反（虽然我们已经检查了，但作为双重保险）
            Map<String, Object> error = new HashMap<>();
            error.put("message", "该职务下还有员工，无法删除。请先将员工转移到其他职务或删除员工后再删除职务。");
            error.put("success", false);
            return ResponseEntity.status(400).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "删除职务配置失败: " + e.getMessage());
            error.put("success", false);
            return ResponseEntity.status(500).body(error);
        }
    }

    // 生成职务ID（格式：P000000001）
    private String generatePositionId() {
        List<PositionConfig> all = positionConfigService.getAllPositionConfigs();
        int maxNum = 0;
        for (PositionConfig pos : all) {
            String pid = pos.getPid();
            if (pid != null && pid.startsWith("P") && pid.length() == 10) {
                try {
                    int num = Integer.parseInt(pid.substring(1));
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException e) {
                    // 忽略
                }
            }
        }
        return String.format("P%09d", maxNum + 1);
    }

    // 格式化Time为HH:mm字符串
    private String formatTime(Time time) {
        if (time == null) return "09:00";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(time);
    }

    // ========== 考勤汇总 ==========
    @GetMapping("/attendance")
    public ResponseEntity<List<Map<String, Object>>> getAttendanceSummary(
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> records = new ArrayList<>();
        return ResponseEntity.ok(records);
    }

    // ========== 审批管理 ==========
    @PostMapping("/applications/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveApplication(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            // 从数据库查询实际的申请记录
            Application applicationRecord = applicationService.getApplicationById(id.intValue());
            if (applicationRecord == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "申请不存在");
                return ResponseEntity.status(404).body(error);
            }

            // 从请求中获取审批人ID
            Integer approverId = null;
            Object approverIdObj = request.get("approverId");
            if (approverIdObj instanceof Number) {
                approverId = ((Number) approverIdObj).intValue();
            } else if (approverIdObj instanceof String) {
                try {
                    approverId = Integer.parseInt((String) approverIdObj);
                } catch (NumberFormatException e) {
                    // 解析失败，使用默认值
                }
            }
            
            // 如果无法获取审批人ID，返回错误
            if (approverId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "无法获取审批人信息，请重新登录");
                return ResponseEntity.status(401).body(error);
            }
            
            // 如果是补卡申请且审批通过，需要更新考勤记录
            boolean approved = (Boolean) request.getOrDefault("approved", false);
            
            // 调用服务更新申请状态
            if (approved) {
                applicationService.approveApplication(id.intValue(), approverId);
            } else {
                String rejectReason = (String) request.getOrDefault("reason", "审批未通过");
                applicationService.rejectApplication(id.intValue(), approverId, rejectReason);
            }
            
            // 重新查询更新后的申请记录
            Application updatedApplication = applicationService.getApplicationById(id.intValue());
            
            Map<String, Object> application = new HashMap<>();
            application.put("id", id);
            application.put("employeeId", updatedApplication.getEid());
            application.put("type", updatedApplication.getApplicationType());
            application.put("startTime", updatedApplication.getStartTime());
            application.put("endTime", updatedApplication.getEndTime());
            application.put("reason", updatedApplication.getReason());
            application.put("status", updatedApplication.getStatus());
            application.put("approverId", updatedApplication.getApproverId());
            application.put("approveTime", updatedApplication.getApproveTime());
            application.put("rejectReason", updatedApplication.getRejectReason());
            
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "审批失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}

