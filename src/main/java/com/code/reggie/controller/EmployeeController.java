package com.code.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.code.reggie.common.R;
import com.code.reggie.entity.Employee;
import com.code.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录
     * @param request 请求
     * @param employee 用户
     * @return R
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        // 对密码进行md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        // 根据用户名和密码查询用户
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee serviceOne = employeeService.getOne(queryWrapper);
        // 判断用户是否存在
        if(serviceOne == null){
            return R.error("用户名不存在");
        }
        if(!serviceOne.getPassword().equals(password)){
            return R.error("密码错误");
        }
        if(serviceOne.getStatus() == 0){
            return R.error("账号已被禁用");
        }
        request.getSession().setAttribute("employee", serviceOne.getId());

        return R.success(serviceOne);
    }

    /**
     * 退出登录
     * @param request 请求
     * @return R
     */
    @PostMapping("/logout")
    public R logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success(null);
    }


    @GetMapping("/page")
    public R<Page<Employee>> list(int page, int pageSize,String name) {
        log.info("page:{},pageSize:{},name: {}", page, pageSize,name);
        Page<Employee> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null,Employee::getName, name);
        queryWrapper.orderByDesc(Employee::getCreateTime);
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        // 设置初始密码
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        employeeService.updateById(employee);
        log.info("update当前线程: {}", Thread.currentThread().getId());
        return R.success("修改员工成功");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        return R.success(employee);
    }
}
