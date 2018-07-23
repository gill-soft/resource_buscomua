//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.07.17 at 01:13:55 PM EEST 
//


package com.gillsoft.client;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


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
 *         &lt;element name="returnStatement">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="AsUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="rulesReturn">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="rule" maxOccurs="unbounded" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;simpleContent>
 *                                   &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                                     &lt;attribute name="code" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                     &lt;attribute name="count" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                   &lt;/extension>
 *                                 &lt;/simpleContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="money" type="{}moneyType"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
    protected CancelResponse.ReturnStatement returnStatement;
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
     *     {@link CancelResponse.ReturnStatement }
     *     
     */
    public CancelResponse.ReturnStatement getReturnStatement() {
        return returnStatement;
    }

    /**
     * Sets the value of the returnStatement property.
     * 
     * @param value
     *     allowed object is
     *     {@link CancelResponse.ReturnStatement }
     *     
     */
    public void setReturnStatement(CancelResponse.ReturnStatement value) {
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="AsUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="rulesReturn">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="rule" maxOccurs="unbounded" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;simpleContent>
     *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
     *                           &lt;attribute name="code" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                           &lt;attribute name="count" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                         &lt;/extension>
     *                       &lt;/simpleContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="money" type="{}moneyType"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "asUID",
        "rulesReturn",
        "money"
    })
    public static class ReturnStatement {

        @XmlElement(name = "AsUID", required = true)
        protected String asUID;
        @XmlElement(required = true)
        protected CancelResponse.ReturnStatement.RulesReturn rulesReturn;
        @XmlElement(required = true)
        protected MoneyType money;

        /**
         * Gets the value of the asUID property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getAsUID() {
            return asUID;
        }

        /**
         * Sets the value of the asUID property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAsUID(String value) {
            this.asUID = value;
        }

        /**
         * Gets the value of the rulesReturn property.
         * 
         * @return
         *     possible object is
         *     {@link CancelResponse.ReturnStatement.RulesReturn }
         *     
         */
        public CancelResponse.ReturnStatement.RulesReturn getRulesReturn() {
            return rulesReturn;
        }

        /**
         * Sets the value of the rulesReturn property.
         * 
         * @param value
         *     allowed object is
         *     {@link CancelResponse.ReturnStatement.RulesReturn }
         *     
         */
        public void setRulesReturn(CancelResponse.ReturnStatement.RulesReturn value) {
            this.rulesReturn = value;
        }

        /**
         * Gets the value of the money property.
         * 
         * @return
         *     possible object is
         *     {@link MoneyType }
         *     
         */
        public MoneyType getMoney() {
            return money;
        }

        /**
         * Sets the value of the money property.
         * 
         * @param value
         *     allowed object is
         *     {@link MoneyType }
         *     
         */
        public void setMoney(MoneyType value) {
            this.money = value;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="rule" maxOccurs="unbounded" minOccurs="0">
         *           &lt;complexType>
         *             &lt;simpleContent>
         *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
         *                 &lt;attribute name="code" type="{http://www.w3.org/2001/XMLSchema}string" />
         *                 &lt;attribute name="count" type="{http://www.w3.org/2001/XMLSchema}string" />
         *               &lt;/extension>
         *             &lt;/simpleContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "rule"
        })
        public static class RulesReturn {

            protected List<CancelResponse.ReturnStatement.RulesReturn.Rule> rule;

            /**
             * Gets the value of the rule property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the rule property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getRule().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link CancelResponse.ReturnStatement.RulesReturn.Rule }
             * 
             * 
             */
            public List<CancelResponse.ReturnStatement.RulesReturn.Rule> getRule() {
                if (rule == null) {
                    rule = new ArrayList<CancelResponse.ReturnStatement.RulesReturn.Rule>();
                }
                return this.rule;
            }


            /**
             * <p>Java class for anonymous complex type.
             * 
             * <p>The following schema fragment specifies the expected content contained within this class.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;simpleContent>
             *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
             *       &lt;attribute name="code" type="{http://www.w3.org/2001/XMLSchema}string" />
             *       &lt;attribute name="count" type="{http://www.w3.org/2001/XMLSchema}string" />
             *     &lt;/extension>
             *   &lt;/simpleContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "value"
            })
            public static class Rule {

                @XmlValue
                protected String value;
                @XmlAttribute(name = "code")
                protected String code;
                @XmlAttribute(name = "count")
                protected String count;

                /**
                 * Gets the value of the value property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getValue() {
                    return value;
                }

                /**
                 * Sets the value of the value property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setValue(String value) {
                    this.value = value;
                }

                /**
                 * Gets the value of the code property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getCode() {
                    return code;
                }

                /**
                 * Sets the value of the code property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setCode(String value) {
                    this.code = value;
                }

                /**
                 * Gets the value of the count property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getCount() {
                    return count;
                }

                /**
                 * Sets the value of the count property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setCount(String value) {
                    this.count = value;
                }

            }

        }

    }

}
