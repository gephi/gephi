package org.gephi.desktop.attributes.edit;

interface AttributeValueWrapper {

    public Byte getValueByte();

    public void setValueByte(Byte object);

    public Short getValueShort();

    public void setValueShort(Short object);

    public Character getValueCharacter();

    public void setValueCharacter(Character object);

    public String getValueString();

    public void setValueString(String object);

    public Double getValueDouble();

    public void setValueDouble(Double object);

    public Float getValueFloat();

    public void setValueFloat(Float object);

    public Integer getValueInteger();

    public void setValueInteger(Integer object);

    public Boolean getValueBoolean();

    public void setValueBoolean(Boolean object);

    public Long getValueLong();

    public void setValueLong(Long object);

    /**
     * **** Other types are not supported by property editors by default so they are used and parsed as Strings *****
     */
    public String getValueAsString();

    public void setValueAsString(String value);
}
