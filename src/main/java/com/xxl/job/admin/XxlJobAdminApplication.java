package com.xxl.job.admin;

import groovy.util.logging.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * @author xuxueli 2018-10-28 00:38:13
 */
@Slf4j
@SpringBootApplication
public class XxlJobAdminApplication {

	private static final Logger log = LoggerFactory.getLogger(XxlJobAdminApplication.class);

	public static void main(String[] args) {
        SpringApplication.run(XxlJobAdminApplication.class, args);
		log.info("XxlJob Admin 启动成功...");
	}

}
