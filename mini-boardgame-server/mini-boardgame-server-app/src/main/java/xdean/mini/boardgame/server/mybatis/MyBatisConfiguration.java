package xdean.mini.boardgame.server.mybatis;

import java.util.Collections;
import java.util.List;

import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xdean.mini.boardgame.server.mybatis.mapper.GameMapper;
import xdean.mini.boardgame.server.mybatis.mapper.UserMapper;
import xdean.mybatis.extension.ConfigurationInitializer;
import xdean.mybatis.extension.MyBatisExtension;

@Component
public class MyBatisConfiguration implements ConfigurationCustomizer {

  @Autowired(required = false)
  List<ConfigurationInitializer> initializers = Collections.emptyList();

  @Override
  public void customize(Configuration configuration) {
    initializers.forEach(i -> i.init(configuration));
    MyBatisExtension.loadMapper(configuration, UserMapper.class);
    MyBatisExtension.loadMapper(configuration, GameMapper.class);
  }
}
