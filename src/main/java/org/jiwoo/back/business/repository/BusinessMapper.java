package org.jiwoo.back.business.repository;

import org.apache.ibatis.annotations.Mapper;
import org.jiwoo.back.business.aggregate.entity.Business;

@Mapper
public interface BusinessMapper {
    Business findById(int id);
}
