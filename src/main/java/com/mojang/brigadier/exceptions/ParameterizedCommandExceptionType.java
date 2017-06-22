package com.mojang.brigadier.exceptions;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParameterizedCommandExceptionType implements CommandExceptionType {
    private static Pattern PATTERN = Pattern.compile("\\$\\{(\\w+)}");
    private static final Joiner JOINER = Joiner.on(", ");

    private final String name;
    private final String message;
    private final String[] keys;

    public ParameterizedCommandExceptionType(String name, String message, String... keys) {
        this.name = name;
        this.message = message;
        this.keys = keys;
    }

    @Override
    public String getTypeName() {
        return name;
    }

    @Override
    public String getErrorMessage(CommandException exception) {
        final Matcher matcher = PATTERN.matcher(message);
        final StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, exception.getData().get(matcher.group(1)).toString());
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public CommandException create(Object... values) {
        if (values.length != keys.length) {
            throw new IllegalArgumentException("Invalid values! (Expected: " + JOINER.join(keys) + ")");
        }

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        for (int i = 0; i < keys.length; i++) {
            builder = builder.put(keys[i], values[i]);
        }

        return new CommandException(this, builder.build());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommandExceptionType)) return false;

        CommandExceptionType that = (CommandExceptionType) o;

        return getTypeName().equals(that.getTypeName());
    }

    @Override
    public int hashCode() {
        return getTypeName().hashCode();
    }
}