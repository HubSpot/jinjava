package com.hubspot.jinjava.el;

import jakarta.el.ELContext;
import jakarta.el.ELException;

import com.hubspot.jinjava.el.misc.TypeConverter;

import java.text.MessageFormat;

/**
 * Object wrapper expression.
 *
 * @author Christoph Beck
 */
public final class ObjectValueExpression extends jakarta.el.ValueExpression {
    private static final long serialVersionUID = 1L;

    private final TypeConverter converter;
    private final Object object;
    private final Class<?> type;

    /**
     * Wrap an object into a value expression.
     * @param converter type converter
     * @param object the object to wrap
     * @param type the expected type this object will be coerced in {@link #getValue(ELContext)}.
     */
    public ObjectValueExpression(TypeConverter converter, Object object, Class<?> type) {
        super();

        this.converter = converter;
        this.object = object;
        this.type = type;

        if (type == null) {
            throw new NullPointerException("Expected type must not be null");
        }
    }

    /**
     * Two object value expressions are equal if and only if their wrapped objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass() == getClass()) {
            ObjectValueExpression other = (ObjectValueExpression)obj;
            if (type != other.type) {
                return false;
            }
            return object == other.object || object != null && object.equals(other.object);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return object == null ? 0 : object.hashCode();
    }

    /**
     * Answer the wrapped object, coerced to the expected type.
     */
    @Override
    public Object getValue(ELContext context) {
        return converter.convert(object, type);
    }

    /**
     * Answer <code>null</code>.
     */
    @Override
    public String getExpressionString() {
        return null;
    }

    /**
     * Answer <code>false</code>.
     */
    @Override
    public boolean isLiteralText() {
        return false;
    }

    /**
     * Answer <code>null</code>.
     */
    @Override
    public Class<?> getType(ELContext context) {
        return null;
    }

    /**
     * Answer <code>true</code>.
     */
    @Override
    public boolean isReadOnly(ELContext context) {
        return true;
    }

    /**
     * Throw an exception.
     */
    @Override
    public void setValue(ELContext context, Object value) {
        throw new ELException(MessageFormat.format("error.value.set.rvalue", "<object value expression>"));
    }

    @Override
    public String toString() {
        return "ValueExpression(" + object + ")";
    }

    @Override
    public Class<?> getExpectedType() {
        return type;
    }
}
