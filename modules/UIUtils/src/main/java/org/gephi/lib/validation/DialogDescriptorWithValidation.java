package org.gephi.lib.validation;

import org.netbeans.validation.api.Problem;
import org.netbeans.validation.api.ui.ValidationUI;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.openide.DialogDescriptor;

public class DialogDescriptorWithValidation implements ValidationUI {

    private final DialogDescriptor dialogDescriptor;

    private DialogDescriptorWithValidation(DialogDescriptor dialogDescriptor) {
        this.dialogDescriptor = dialogDescriptor;
    }

    public static DialogDescriptor dialog(Object innerPane, String title) {
        DialogDescriptor dd = new DialogDescriptor(innerPane, title);
        if (innerPane instanceof ValidationPanel) {
            ((ValidationPanel) innerPane).getValidationGroup().addUI(new DialogDescriptorWithValidation(dd));
        }
        return dd;
    }

    @Override
    public void showProblem(Problem problem) {
        dialogDescriptor.setValid(!problem.isFatal());
    }

    @Override
    public void clearProblem() {
        dialogDescriptor.setValid(true);
    }
}
