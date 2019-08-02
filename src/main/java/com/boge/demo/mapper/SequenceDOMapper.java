package com.boge.demo.mapper;

import com.boge.demo.dataobject.SequenceDO;
import org.springframework.stereotype.Repository;

import java.sql.Date;

@Repository
public interface SequenceDOMapper {
    int deleteByPrimaryKey(String name);

    int insert(SequenceDO record);

    int insertSelective(SequenceDO record);

    SequenceDO selectByPrimaryKey(String name);

    int updateByPrimaryKeySelective(SequenceDO record);

    int updateByPrimaryKey(SequenceDO record);

}