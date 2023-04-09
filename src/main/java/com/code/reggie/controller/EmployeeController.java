package com.code.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.code.reggie.common.R;
import com.code.reggie.entity.Employee;
import com.code.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    /**
     * 用户列表
     * @return R
     * @param page 页码
     * @param pageSize 每页显示条数
     */
    @GetMapping("/page")
    public R<List<Employee>> list(@Param("page") Integer page, @Param("pageSize") Integer pageSize) {
        Page<Employee> iPage = new Page<>(page, pageSize);
        List<Employee> list = employeeService.page(iPage).getRecords();
        return R.success(list);
    }
}
