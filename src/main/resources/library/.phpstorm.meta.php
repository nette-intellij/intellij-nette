<?php

namespace PHPSTORM_META {

	override(
		\Nette\DI\Container::getService(0),
		map( [
			'application' => \Nette\Application\Application::class,
		])
	);

	override(
		\Nette\DI\Container::getByType(0),
		map([
			'' => '@',
		])
	);

	override(\Nette\Utils\Arrays::get(0), elementType(0));
	override(\Nette\Utils\Arrays::getRef(0), elementType(0));
	override(\Nette\Utils\Arrays::grep(0), type(0));

	exitPoint(\Nette\Application\UI\Presenter::terminate());
	exitPoint(\Nette\Application\UI\Presenter::sendResponse());
	exitPoint(\Nette\Application\UI\Presenter::sendJson());
	exitPoint(\Nette\Application\UI\Presenter::sendTemplate());
	exitPoint(\Nette\Application\UI\Presenter::sendPayload());
	exitPoint(\Nette\Application\UI\Presenter::forward());
	exitPoint(\Nette\Application\UI\Presenter::redirect());
	exitPoint(\Nette\Application\UI\Presenter::redirectUrl());
	exitPoint(\Nette\Application\UI\Presenter::restoreRequest());
}