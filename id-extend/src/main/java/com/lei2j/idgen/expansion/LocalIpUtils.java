package com.lei2j.idgen.expansion;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

/**
 * @author leijinjun
 * @date 2022/11/17
 **/
public class LocalIpUtils {

    /**
     * @param interfaceName 指定网卡名称
     * @return
     */
    public static String getLocalIp(String interfaceName) {
        try {
            final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface targetInterface = null;
            NetworkInterface targetInterface1 = null;
            while (networkInterfaces.hasMoreElements()) {
                final NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isLoopback()
                        || !networkInterface.isUp()) {
                    continue;
                }
                if (networkInterface.getName().equalsIgnoreCase(interfaceName)
                        ||networkInterface.getDisplayName().equalsIgnoreCase(interfaceName)) {
                    targetInterface = networkInterface;
                    break;
                }
                if (targetInterface1 == null) {
                    targetInterface1 = networkInterface;
                }
            }
            if (targetInterface == null) {
                targetInterface = targetInterface1;
            }
            if (targetInterface != null) {
                final List<InterfaceAddress> interfaceAddresses = targetInterface.getInterfaceAddresses();
                return interfaceAddresses.stream().filter(p -> Objects.nonNull(p.getAddress()))
                        .filter(p->p.getAddress() instanceof Inet4Address)
                        .map(c -> c.getAddress().getHostAddress())
                        .findFirst().orElse(null);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
