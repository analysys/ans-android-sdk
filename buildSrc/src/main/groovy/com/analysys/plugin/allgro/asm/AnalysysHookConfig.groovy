package com.analysys.plugin.allgro.asm


import org.objectweb.asm.Opcodes

class AnalysysHookConfig {
    public static final String ASM_PROBE_HELP = 'com/analysys/allgro/plugin/ASMProbeHelp'
    /**
     * Fragment中的方法
     */
    public final static HashMap<String, AnalysysMethodCell> PV_METHODS = new HashMap<>()

    static {
        addPVMethod(new AnalysysMethodCell(
                '',
                'onViewCreated',
                '(Landroid/view/View;Landroid/os/Bundle;)V',
                0, [Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ALOAD],
                'onFragmentViewCreated',
                '(Ljava/lang/Object;Landroid/view/View;Landroid/os/Bundle;Z)V'))

        addPVMethod(new AnalysysMethodCell(
                '',
                'onResume',
                '()V',
                0, [Opcodes.ALOAD],
                'trackFragmentResume',
                '(Ljava/lang/Object;Z)V'))

        addPVMethod(new AnalysysMethodCell(
                '',
                'setUserVisibleHint',
                '(Z)V',
                0, [Opcodes.ALOAD, Opcodes.ILOAD],
                'trackFragmentSetUserVisibleHint',
                '(Ljava/lang/Object;ZZ)V'))

        addPVMethod(new AnalysysMethodCell(
                '',
                'onHiddenChanged',
                '(Z)V',
                0, [Opcodes.ALOAD, Opcodes.ILOAD],
                'trackOnHiddenChanged',
                '(Ljava/lang/Object;ZZ)V'))

    }

    static void addPVMethod(AnalysysMethodCell methodCell) {
        if (methodCell != null) {
            PV_METHODS.put(methodCell.mName + methodCell.mDesc, methodCell)
        }
    }


    public final static def CLICK_METHODS = [:]
    public final static CLICK_METHODS_LAMBDA = [:]
    static {
        def CLICK_HOOK = []
        // commonView click
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Landroid/view/View$OnClickListener;',
                'onClick',
                '(Landroid/view/View;)V',
                1, [Opcodes.ALOAD],
                'trackViewOnClick',
                '(Landroid/view/View;Z)V'))
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Landroid/widget/CompoundButton$OnCheckedChangeListener;',
                'onCheckedChanged',
                '(Landroid/widget/CompoundButton;Z)V',
                1, [Opcodes.ALOAD],
                'trackViewOnClick',
                '(Landroid/view/View;Z)V'))
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Landroid/widget/RatingBar$OnRatingBarChangeListener;',
                'onRatingChanged',
                '(Landroid/widget/RatingBar;FZ)V',
                1, [Opcodes.ALOAD],
                'trackViewOnClick',
                '(Landroid/view/View;Z)V'))
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Landroid/widget/SeekBar$OnSeekBarChangeListener;',
                'onStopTrackingTouch',
                '(Landroid/widget/SeekBar;)V',
                1, [Opcodes.ALOAD],
                'trackViewOnClick',
                '(Landroid/view/View;Z)V'))

        // 拓展兼容控件
        // Radio
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Landroid/widget/RadioGroup$OnCheckedChangeListener;',
                'onCheckedChanged',
                '(Landroid/widget/RadioGroup;I)V',
                1, [Opcodes.ALOAD, Opcodes.ILOAD],
                'trackRadioGroup',
                '(Landroid/widget/RadioGroup;IZ)V'))

        // AdapterView
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Landroid/widget/AdapterView$OnItemClickListener;',
                'onItemClick',
                '(Landroid/widget/AdapterView;Landroid/view/View;IJ)V',
                1, [Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD],
                'trackListView',
                '(Landroid/widget/AdapterView;Landroid/view/View;IZ)V'))
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Landroid/widget/AdapterView$OnItemClickListener;',
                'onItemClick',
                '(Landroid/widget/ListView;Landroid/view/View;IJ)V',
                1, [Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD],
                'trackListView',
                '(Landroid/widget/AdapterView;Landroid/view/View;IZ)V'))
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Landroid/widget/AdapterView$OnItemSelectedListener;',
                'onItemSelected',
                '(Landroid/widget/AdapterView;Landroid/view/View;IJ)V',
                1, [Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD],
                'trackListView',
                '(Landroid/widget/AdapterView;Landroid/view/View;IZ)V')) //  1
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Landroid/widget/ExpandableListView$OnGroupClickListener;',
                'onGroupClick',
                '(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z',
                1, [Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD],
                'trackExpListViewGroupClick',
                '(Landroid/widget/ExpandableListView;Landroid/view/View;IZ)V'))
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Landroid/widget/ExpandableListView$OnChildClickListener;',
                'onChildClick',
                '(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z',
                1, [Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.ILOAD],
                'trackExpListViewChildClick',
                '(Landroid/widget/ExpandableListView;Landroid/view/View;IIZ)V'))

        // Tab
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Landroid/widget/TabHost$OnTabChangeListener;',
                'onTabChanged',
                '(Ljava/lang/String;)V',
                1, [Opcodes.ALOAD],
                'trackTabHost',
                '(Ljava/lang/String;Z)V'))
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Landroid/support/design/widget/TabLayout$OnTabSelectedListener;',
                'onTabSelected',
                '(Landroid/support/design/widget/TabLayout$Tab;)V',
                0, [Opcodes.ALOAD, Opcodes.ALOAD],
                'trackTabLayout',
                '(Ljava/lang/Object;Ljava/lang/Object;Z)V'))// 2
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Lcom/google/android/material/tabs/TabLayout$OnTabSelectedListener;',
                'onTabSelected',
                '(Lcom/google/android/material/tabs/TabLayout$Tab;)V',
                0, [Opcodes.ALOAD, Opcodes.ALOAD],
                'trackTabLayout',
                '(Ljava/lang/Object;Ljava/lang/Object;Z)V'))//1
        // Menu
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Landroid/widget/Toolbar$OnMenuItemClickListener;',
                'onMenuItemClick',
                '(Landroid/view/MenuItem;)Z',
                1, [Opcodes.ALOAD],
                'trackMenuItem',
                '(Landroid/view/MenuItem;Z)V'))
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Landroid/support/v7/widget/Toolbar$OnMenuItemClickListener;',
                'onMenuItemClick',
                '(Landroid/view/MenuItem;)Z',
                1, [Opcodes.ALOAD],
                'trackMenuItem',
                '(Landroid/view/MenuItem;Z)V'))
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Landroidx/appcompat/widget/Toolbar$OnMenuItemClickListener;',
                'onMenuItemClick',
                '(Landroid/view/MenuItem;)Z',
                1, [Opcodes.ALOAD],
                'trackMenuItem',
                '(Landroid/view/MenuItem;Z)V'))
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Landroid/support/design/widget/NavigationView$OnNavigationItemSelectedListener;',
                'onMenuItemClick',
                '(Landroid/view/MenuItem;)Z',
                1, [Opcodes.ALOAD],
                'trackMenuItem',
                '(Landroid/view/MenuItem;Z)V'))
        // Menu2
        CLICK_HOOK.add(new AnalysysMethodCell(
                '',
                'onContextItemSelected',
                '(Landroid/view/MenuItem;)Z',
                0, [Opcodes.ALOAD, Opcodes.ALOAD],
                'trackMenuItem',
                '(Ljava/lang/Object;Landroid/view/MenuItem;Z)V'))
        CLICK_HOOK.add(new AnalysysMethodCell(
                '',
                'onOptionsItemSelected',
                '(Landroid/view/MenuItem;)Z',
                0, [Opcodes.ALOAD, Opcodes.ALOAD],
                'trackMenuItem',
                '(Ljava/lang/Object;Landroid/view/MenuItem;Z)V'))
        CLICK_HOOK.add(new AnalysysMethodCell(
                '',
                'onNavigationItemSelected',
                '(Landroid/view/MenuItem;)Z',
                0, [Opcodes.ALOAD, Opcodes.ALOAD],
                'trackMenuItem',
                '(Ljava/lang/Object;Landroid/view/MenuItem;Z)V'))

        // Dialog
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Landroid/content/DialogInterface$OnMultiChoiceClickListener;',
                'onClick',
                '(Landroid/content/DialogInterface;IZ)V',
                1, [Opcodes.ALOAD, Opcodes.ILOAD],
                'trackDialog',
                '(Landroid/content/DialogInterface;IZ)V'))
        CLICK_HOOK.add(new AnalysysMethodCell(
                'Landroid/content/DialogInterface$OnClickListener;',
                'onClick',
                '(Landroid/content/DialogInterface;I)V',
                1, [Opcodes.ALOAD, Opcodes.ILOAD],
                'trackDialog',
                '(Landroid/content/DialogInterface;IZ)V'))

        CLICK_HOOK.each {
            AnalysysMethodCell cell ->
                CLICK_METHODS.put(cell.mName + cell.mDesc, cell)
                CLICK_METHODS_LAMBDA.put(cell.mOwner + cell.mName + cell.mDesc, cell)
        }
    }


}