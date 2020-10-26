package com.enliple.ar.dao;

import com.enliple.ar.common.Config;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class RedisCluster {
	
	@Autowired
	private Config config;
	
	@Getter private JedisCluster jc;
	private Set<HostAndPort> hosts;

	@PostConstruct
	private void init() {
		hosts = new HashSet<HostAndPort>();

		for( String node : config.getRedisHosts()) {
			log.info("Redis Host :: {}", node);
			String[] e = node.split(":");
			if( e.length != 2) continue;
			String ip = e[0];
			int port = Integer.valueOf( e[1]);
			hosts.add(new HostAndPort(ip, port));

		}
		
	    JedisPoolConfig config = new JedisPoolConfig();
	    config.setMaxTotal(500);
	    config.setMaxIdle(1000 * 60);
	    
	    config.setMaxWaitMillis(5000);
	    config.setTestOnBorrow(true);
	    
		jc = new JedisCluster(hosts, 5000, 1000, 1, config);
	}
	
	@PreDestroy
	public void close () {
		log.info("Redis shutdowing");
		if( jc != null)
			try {
				jc.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		log.info("Redis shutdowned");
	}
	
	public void saveData() {
		for( HostAndPort host: hosts) {
			Jedis jedis = new Jedis( host.getHost(), host.getPort());
			jedis.bgsave();
			jedis.close();
		}
	}
	
	// 플랫폼 에 대한 구분 코드
	public String getCodeForPlatform(String mode, boolean isPc, boolean isClick) {
		
		String code = "";
		
		if(mode.equals("rating")) {
			code = "";
		}else if(mode.equals("sim")) {		
			if( isClick) code = isPc ?  "1" : "2";
			else code = isPc ?  "1" : "2";
		}else {
			if( isClick) code = isPc ?  "5" : "6";
			else code = isPc ?  "7" : "8";
		}
		
		return code;
	}	

	public String getRedisKey(String preText, String mode, boolean isPc, String advId, boolean isClick, String secondKey) {
		String code = getCodeForPlatform(mode, isPc, isClick);		
		String redisKey = String.format( "%s%s_%s_%s", preText, code, advId, secondKey);
		return redisKey;
	}
	

	public String saveRedis(String key, String value, int expireTime) {
		//String code = getCodeForPlatform(mode, isPc);		
		//String key = String.format( "%s%s_%s_%s", preText, code, advId, strAuidOrPcode);
		try {
			getJc().setex(key, expireTime , value);
		} catch( Exception e) {
			log.error("RedisException : key="+key, e);
		}
		return key;
 	}
	
	public String saveRedisMap(String key, Map<String,String> valueMap, int expireTime) {
		//String key = this.getRedisKey(preText, mode, isPc, advId, strAuidOrPcode);
		try {
			getJc().hmset(key, valueMap);
			getJc().expire(key, expireTime);
		} catch( Exception e) {
			log.error("RedisException : key="+key, e);
		}
		
		return key;
 	}	
}
