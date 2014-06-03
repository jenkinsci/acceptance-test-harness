<?php
$config->parentPath("/home");
$config->addRepository('root', 'file:///home/svn');
$config->setTemplatePath("$locwebsvnreal/templates/calm/");
$config->setMinDownloadLevel(2);
set_time_limit(0);
$config->expandTabsBy(8);
?>