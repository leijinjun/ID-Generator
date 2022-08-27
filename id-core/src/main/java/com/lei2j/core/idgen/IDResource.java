package com.lei2j.core.idgen;

import java.util.function.Supplier;

/**
 * ID资源接口
 * @author leijinjun
 **/

@FunctionalInterface
public interface IDResource extends Supplier<ID> {

}
