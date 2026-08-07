package com.worketa.common.security;

import java.util.UUID;

public final class OrganisationContext {
    private static final ThreadLocal<UUID> current = new ThreadLocal<>();

    public static void set(UUID id) { current.set(id); }
    public static UUID get() { return current.get(); }
    public static void clear() { current.remove(); }
}
