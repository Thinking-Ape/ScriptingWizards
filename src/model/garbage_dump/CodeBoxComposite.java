package view;

import java.util.List;

@Deprecated
public interface CodeBoxComposite {
    String getText();

    List<String> getAllCode();

    CodeBoxCompound getParentCodeBox();
}
