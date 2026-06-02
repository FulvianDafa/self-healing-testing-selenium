package com.fulvian.utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class DomMutationHelper {

    private final JavascriptExecutor js;

    public DomMutationHelper(WebDriver driver) {
        this.js = (JavascriptExecutor) driver;
    }

    public void changeId(String oldId, String newId) {
        Object changed = js.executeScript(
                "const el = document.getElementById(arguments[0]);" +
                "if (!el) return false;" +
                "el.setAttribute('id', arguments[1]);" +
                "return true;",
                oldId, newId
        );

        validateChange(changed, "id", oldId, newId);
    }

    public void changeClassById(String elementId, String newClassValue) {
        Object changed = js.executeScript(
                "const el = document.getElementById(arguments[0]);" +
                "if (!el) return false;" +
                "el.setAttribute('class', arguments[1]);" +
                "return true;",
                elementId, newClassValue
        );

        validateChange(changed, "class", elementId, newClassValue);
    }

    public void changeAttributeById(String elementId, String attributeName, String newValue) {
        Object changed = js.executeScript(
                "const el = document.getElementById(arguments[0]);" +
                "if (!el) return false;" +
                "el.setAttribute(arguments[1], arguments[2]);" +
                "return true;",
                elementId, attributeName, newValue
        );

        validateChange(changed, attributeName, elementId, newValue);
    }

    public void removeAttributeById(String elementId, String attributeName) {
        Object changed = js.executeScript(
                "const el = document.getElementById(arguments[0]);" +
                "if (!el) return false;" +
                "el.removeAttribute(arguments[1]);" +
                "return true;",
                elementId, attributeName
        );

        validateChange(changed, "remove " + attributeName, elementId, "");
    }

    public void wrapElementById(String elementId, String wrapperClassName) {
        Object changed = js.executeScript(
                "const el = document.getElementById(arguments[0]);" +
                "if (!el) return false;" +
                "const wrapper = document.createElement('div');" +
                "wrapper.setAttribute('class', arguments[1]);" +
                "el.parentNode.insertBefore(wrapper, el);" +
                "wrapper.appendChild(el);" +
                "return true;",
                elementId, wrapperClassName
        );

        validateChange(changed, "wrap", elementId, wrapperClassName);
    }

    private void validateChange(Object changed, String mutationType, String target, String newValue) {
        if (!Boolean.TRUE.equals(changed)) {
            throw new IllegalStateException(
                    "Precondition gagal: elemen target '" + target + "' tidak ditemukan untuk mutation: " + mutationType
            );
        }

        System.out.printf(
                "[DomMutationHelper] DOM diubah | target='%s' | mutation='%s' | newValue='%s'%n",
                target,
                mutationType,
                newValue
        );
    }
}