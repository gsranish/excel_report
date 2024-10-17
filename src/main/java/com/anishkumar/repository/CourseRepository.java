package com.anishkumar.repository;

import java.io.Serializable;

import com.anishkumar.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Serializable> {

}