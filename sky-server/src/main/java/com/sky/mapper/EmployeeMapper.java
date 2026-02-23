package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    @Insert("INSERT into employee (name, username, password, phone, sex, id_number, status,create_time, update_time,create_user, update_user)" +
            "VALUES " +
            "(#{name}, #{username}, #{password}, #{phone},#{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @AutoFill(OperationType.INSERT)
    void addEmployee(Employee employee);

    Page<Employee> page(EmployeePageQueryDTO employeePageQueryDTO);
    
    @Update("update employee SET name = #{name}, username = #{username}, phone = #{phone}, sex = #{sex}, id_number = #{idNumber}, update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    @AutoFill(OperationType.UPDATE)
    void updateEmp(Employee employee);

    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);
}
