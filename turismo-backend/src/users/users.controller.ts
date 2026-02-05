	import { Controller, Get, Patch, Param, Body, UseGuards, Req, Put, UploadedFile, UseInterceptors, Post, Delete } from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { diskStorage } from 'multer';
import { extname } from 'path';
import { UsersService } from './users.service';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { RolesGuard } from '../auth/roles.guard';
import { Roles } from '../auth/roles.decorator';

@UseGuards(JwtAuthGuard, RolesGuard)
@Controller('users')
export class UsersController {
	constructor(private readonly usersService: UsersService) {}

	// Obtener perfil de usuario por id (cualquier usuario autenticado)
	@Get(':id')
	async findOne(@Param('id') id: string, @Req() req: any) {
		const userId = +id;
		const authUser = req.user as any;
		const authUserId = authUser?.userId || authUser?.sub;
		const user = await this.usersService.findOne(userId);
		if (!user) return null;
		
		// Obtener contadores de followers/following
		const followersCount = await this.usersService.getFollowersCount(userId);
		const followingCount = await this.usersService.getFollowingCount(userId);
		
		// Si el usuario autenticado es diferente, incluir isFollowing
		if (authUserId && userId !== +authUserId && this.usersService.isFollowing) {
			const isFollowing = await this.usersService.isFollowing(+authUserId, userId);
			return { ...user, isFollowing, followersCount, followingCount };
		}
		return { ...user, followersCount, followingCount };
	}

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

	// Actualizar perfil del usuario autenticado
	@Put('profile')
	async updateProfile(@Req() req: any, @Body() body: { name?: string; profileImage?: string }) {
		const user = req.user as any;
		const userId = user?.userId || user?.sub;
		return this.usersService.updateProfile(+userId, body);
	}

	// Subir imagen de perfil
	@Put('profile/image')
	@UseInterceptors(FileInterceptor('image', {
		storage: diskStorage({
			destination: './uploads',
			filename: (req, file, cb) => {
				const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1e9);
				cb(null, uniqueSuffix + extname(file.originalname));
			},
		}),
		fileFilter: (req, file, cb) => {
			if (!file.mimetype.match(/^image\//)) {
				return cb(new Error('Only image files are allowed!'), false);
			}
			cb(null, true);
		},
	}))
	async uploadProfileImage(@Req() req: any, @UploadedFile() file: Express.Multer.File) {
		const user = req.user as any;
		const userId = user?.userId || user?.sub;
		const imagePath = file.path.replace('\\', '/');
		return this.usersService.updateProfile(+userId, { profileImage: imagePath });
	}

	// Seguir a un usuario
	@Post('follow/:id')
	async followUser(@Req() req: any, @Param('id') id: string) {
		const user = req.user as any;
		const userId = user?.userId || user?.sub;
		return this.usersService.followUser(+userId, +id);
	}

	// Dejar de seguir a un usuario
	@Delete('follow/:id')
	async unfollowUser(@Req() req: any, @Param('id') id: string) {
		const user = req.user as any;
		const userId = user?.userId || user?.sub;
		return this.usersService.unfollowUser(+userId, +id);
	}
}
