package com.workpool.user.config;

import com.hazelcast.config.*;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.context.SpringManagedContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {

    @Value("${hazelcast.cluster-name:work-pool-cluster}")
    private String clusterName;

    @Value("${hazelcast.cluster-members:127.0.0.1:5701}")
    private String clusterMembers;

    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        config.setClusterName(clusterName);

        NetworkConfig network = config.getNetworkConfig();
        network.setPort(5702).setPortAutoIncrement(true);

        JoinConfig join = network.getJoin();
        join.getMulticastConfig().setEnabled(false);
        TcpIpConfig tcpIp = join.getTcpIpConfig().setEnabled(true);
        for (String member : clusterMembers.split(",")) {
            tcpIp.addMember(member.trim());
        }

        // User session cache: TTL 30 min
        config.addMapConfig(new MapConfig("user-sessions")
                .setTimeToLiveSeconds(1800)
                .setMaxIdleSeconds(600));

        // Skill-location index cache: TTL 5 min
        config.addMapConfig(new MapConfig("skill-location-cache")
                .setTimeToLiveSeconds(300));

        config.setManagedContext(new SpringManagedContext());
        return config;
    }
}
