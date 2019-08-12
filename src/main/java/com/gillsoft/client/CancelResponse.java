//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.08.12 at 08:46:44 AM EEST 
//


package com.gillsoft.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for cancelResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cancelResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="techInfo" type="{}techInfoType"/>
 *         &lt;element name="returnStatement" type="{}returnStatementType"/>
 *         &lt;element name="error" type="{}errorType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cancelResponse", propOrder = {
    "techInfo",
    "returnStatement",
    "error"
})
public class CancelResponse extends BaseResponse {

    @XmlElement(required = true)
    protected TechInfoType techInfo;
    @XmlElement(required = true)
    protected ReturnStatementType returnStatement;
    @XmlElement(required = true)
    protected ErrorType error;

    /**
     * Gets the value of the techInfo property.
     * 
     * @return
     *     possible object is
     *     {@link TechInfoType }
     *     
     */
    public TechInfoType getTechInfo() {
        return techInfo;
    }

    /**
     * Sets the value of the techInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link TechInfoType }
     *     
     */
    public void setTechInfo(TechInfoType value) {
        this.techInfo = value;
    }

    /**
     * Gets the value of the returnStatement property.
     * 
     * @return
     *     possible object is
     *     {@link ReturnStatementType }
     *     
     */
    public ReturnStatementType getReturnStatement() {
        return returnStatement;
    }

    /**
     * Sets the value of the returnStatement property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReturnStatementType }
     *     
     */
    public void setReturnStatement(ReturnStatementType value) {
        this.returnStatement = value;
    }

    /**
     * Gets the value of the error property.
     * 
     * @return
     *     possible object is
     *     {@link ErrorType }
     *     
     */
    public ErrorType getError() {
        return error;
    }

    /**
     * Sets the value of the error property.
     * 
     * @param value
     *     allowed object is
     *     {@link ErrorType }
     *     
     */
    public void setError(ErrorType value) {
        this.error = value;
    }

}
