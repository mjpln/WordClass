/*
 * File: app/store/synonymtypeStore.js
 *
 * This file was generated by Sencha Architect version 3.1.0.
 * http://www.sencha.com/products/architect/
 *
 * This file requires use of the Ext JS 4.0.x library, under independent license.
 * License of Sencha Architect does not include license for Ext JS 4.0.x. For more
 * details see http://www.sencha.com/license or contact license@sencha.com.
 *
 * This file will be auto-generated each and everytime you save your project.
 *
 * Do NOT hand edit this file.
 */

Ext.define('MyApp.store.synonymtypeStore', {
    extend: 'Ext.data.Store',

    requires: [
        'MyApp.model.comboboxModel',
        'Ext.data.proxy.Memory'
    ],

    constructor: function(cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
            model: 'MyApp.model.comboboxModel',
            storeId: 'synonymtypeStore',
            proxy: {
                type: 'memory',
                data: [
                    {
                        id: 1,
                        text: '全称'
                    },
                    {
                        id: 2,
                        text: '简称'
                    },
                    {
                        id: 3,
                        text: '代码'
                    },
                    {
                        id: 4,
                        text: '错词'
                    },
                    {
                        id: 5,
                        text: '其他别名'
                    }
                ]
            }
        }, cfg)]);
    }
});