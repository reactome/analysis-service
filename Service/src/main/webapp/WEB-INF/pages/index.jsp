<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="header.jsp"/>

<div class="wrapper">
    <div class="grid_24">
        <div class="contentwrap" style="margin: 5px 0 5px 0">
            <div class="contenthead">Pathway Analysis Service API</div>
            <div class="contentbody" id="restDocIframeContainer">
                <iframe src="restIframe.html" width="100%" scrolling="no" id="restDocIframe"></iframe>
            </div>
        </div>
    </div>
    <div class="clear"></div>
</div>

</div>            <%--A weird thing to avoid problems--%>
<c:import url="footer.jsp"/>