function ItemTypeService() {
    this.loadAllTypes = function() {
        $.ajax({
            url : "../types",
            type : 'get',
            success : function(data) {
                displayType(data);
            }
        });
    }
}

function displayType(types) {
    console.log(types);
}

$(document).ready(function() {
    new ItemTypeService().loadAllTypes()
})