package com.wenzi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wenzi.entity.Dealer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DealerMapper extends BaseMapper<Dealer> {

    /**
     * 🚀 毕设杀手锏：利用 MySQL 的 ST_Distance_Sphere 函数计算地球球面距离，并自动排序取前 N 名
     */
    @Select("SELECT *, " +
            "ST_Distance_Sphere(point(longitude, latitude), point(#{userLng}, #{userLat})) AS distance " +
            "FROM biz_dealer " +
            "WHERE status = 1 AND brand = #{brand} " +
            "ORDER BY distance ASC ")
    List<Dealer> findNearestDealers(@Param("userLng") Double userLng,
                                    @Param("userLat") Double userLat,
                                    @Param("brand") String brand);
}