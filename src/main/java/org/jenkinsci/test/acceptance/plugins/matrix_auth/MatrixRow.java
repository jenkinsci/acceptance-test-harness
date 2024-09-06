package org.jenkinsci.test.acceptance.plugins.matrix_auth;

import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Represents a single row in the matrix permission table.
 *
 * @author Kohsuke Kawaguchi
 */
public class MatrixRow extends PageAreaImpl {
    public MatrixRow(PageArea area, String relativePath) {
        super(area, relativePath);
    }

    public MatrixRow on(Object... permissions) {
        return _check(true, permissions);
    }

    public MatrixRow off(Object... permissions) {
        return _check(false, permissions);
    }

    public MatrixRow _check(boolean enable, Object... permissions) {
        for (Object p : permissions) {
            if (p != null) {
                control(p.toString()).check(enable);
            }
        }
        return this;
    }

    /**
     * Check the admin permission.
     */
    public MatrixRow admin() {
        return on(ADMINISTER);
    }

    /**
     * Check the read only permission on overall and job.
     */
    public MatrixRow readOnly() {
        return on(OVERALL_READ, ITEM_READ);
    }

    /**
     * Checks the permission necessary to manipulate jobs but without admin right
     */
    public MatrixRow developer() {
        return on(OVERALL_READ, ITEM_READ, ITEM_CREATE, ITEM_DELETE, ITEM_CONFIGURE, ITEM_BUILD, ITEM_CANCEL);
    }

    public static final String ADMINISTER = "hudson.model.Hudson.Administer";
    public static final String OVERALL_READ = "hudson.model.Hudson.Read";
    public static final String ITEM_READ = "hudson.model.Item.Read";
    public static final String ITEM_CREATE = "hudson.model.Item.Create";
    public static final String ITEM_DELETE = "hudson.model.Item.Delete";
    public static final String ITEM_CONFIGURE = "hudson.model.Item.Configure";
    public static final String ITEM_BUILD = "hudson.model.Item.Build";
    public static final String ITEM_CANCEL = "hudson.model.Item.Cancel";
}
