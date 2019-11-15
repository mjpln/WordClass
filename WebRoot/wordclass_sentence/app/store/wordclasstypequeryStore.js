/*
 * File: app/store/wordclasstypequeryStore.js
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

Ext.define('MyApp.store.wordclasstypequeryStore', {
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
            storeId: 'wordclasstypequeryStore',
            proxy: {
                type: 'memory',
                data: [
                    {
                        id: 0,
                        text: '全部'
                    },
                    {
                        id: 1,
                        text: '当前商家'
                    },
                    {
                        id: 2,
                        text: '当前行业'
                    },
                    {
                        id: 3,
                        text: '通用行业'
                    }
                ]
            }
        }, cfg)]);
    }
});