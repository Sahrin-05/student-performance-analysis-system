package com.sahrin.spas.repository;

import com.sahrin.spas.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<Subject> findByActiveTrueOrderByNameAsc();

    List<Subject> findAllByOrderByNameAsc();

    List<Subject> findByCourseIdOrderByNameAsc(Long courseId);

    List<Subject> findByCourseIdAndActiveTrueOrderByNameAsc(Long courseId);

    List<Subject> findByDepartmentIdOrderByNameAsc(Long departmentId);

    boolean existsByCourseId(Long courseId);

    boolean existsByDepartmentId(Long departmentId);
}
