package com.cafepos.gui;

import java.awt.*;

/**
 * FlowLayout subclass that wraps correctly inside a JScrollPane.
 * (Standard FlowLayout in a scroll pane does not reflow on resize.)
 */
public class WrapLayout extends FlowLayout {

    public WrapLayout() { super(); }
    public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        return layoutSize(target, false);
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;
            if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;

            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);

            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            for (int i = 0; i < target.getComponentCount(); i++) {
                Component c = target.getComponent(i);
                if (c.isVisible()) {
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (rowWidth + d.width > maxWidth) {
                        dim.height += rowHeight + vgap;
                        dim.width   = Math.max(dim.width, rowWidth);
                        rowWidth    = 0;
                        rowHeight   = 0;
                    }
                    rowWidth  += d.width + hgap;
                    rowHeight  = Math.max(rowHeight, d.height);
                }
            }
            dim.height += rowHeight + vgap * 2;
            dim.width   = Math.max(dim.width, rowWidth);
            dim.width  += insets.left + insets.right + hgap * 2;
            return dim;
        }
    }
}
