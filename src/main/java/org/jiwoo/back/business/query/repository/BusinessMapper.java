package org.jiwoo.back.business.query.repository;

import org.apache.ibatis.annotations.Mapper;
import org.jiwoo.back.business.query.aggregate.entity.Business;

@Mapper
public interface BusinessMapper {
    Business findById(int id);
}
