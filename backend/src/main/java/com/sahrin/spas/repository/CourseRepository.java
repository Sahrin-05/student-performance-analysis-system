package com.sahrin.spas.repository;

import com.sahrin.spas.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<Course> findByActiveTrueOrderByNameAsc();

    List<Course> findAllByOrderByNameAsc();

    List<Course> findByDepartmentIdOrderByNameAsc(Long departmentId);

    List<Course> findByDepartmentIdAndActiveTrueOrderByNameAsc(Long departmentId);

    boolean existsByDepartmentId(Long departmentId);

    boolean existsByDepartmentIdAndActiveTrue(Long departmentId);
}
