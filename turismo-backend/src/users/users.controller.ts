import { Controller, Get, Patch, Param, Body, UseGuards } from '@nestjs/common';
import { UsersService } from './users.service';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { RolesGuard } from '../auth/roles.guard';
import { Roles } from '../auth/roles.decorator';

@UseGuards(JwtAuthGuard, RolesGuard)
@Controller('users')
export class UsersController {
	constructor(private readonly usersService: UsersService) {}

	// Listar todos los usuarios (cualquier usuario autenticado)
	@Get()
	findAll() {
		return this.usersService.findAll();
	}


	// Bloquear usuario (solo ADMIN)
	@Roles('ADMIN')
	@Patch(':id/block')
	block(@Param('id') id: string) {
		return this.usersService.setActive(+id, false);
	}

	// Desbloquear usuario (solo ADMIN)
	@Roles('ADMIN')
	@Patch(':id/unblock')
	unblock(@Param('id') id: string) {
		return this.usersService.setActive(+id, true);
	}

	// Cambiar rol de usuario (solo ADMIN)
	@Roles('ADMIN')
	@Patch(':id/role')
	changeRole(@Param('id') id: string, @Body('role') role: string) {
		return this.usersService.changeRole(+id, role);
	}
}
